Flink

```
# KAFKA_BROKER=localhost:9092 FLASK_PORT=9999 python webapp.py

from gevent.wsgi     import WSGIServer
from os              import getenv
from uuid            import uuid1
from json            import dumps
from confluent_kafka import Producer as KPR
from datetime        import datetime as dt
from time            import time as t
from flask           import ( render_template as rt
                            , Flask
                            , request
                            , redirect
                            , session )

POSSIBLE_SIGNALS = \
    [ 'aaa', 'aaaa', 'aaaaa', 'aaaaaa'
    , 'bbb', 'bbbb', 'bbbbb', 'bbbbbb'
    , 'ccc', 'cccc', 'ccccc', 'cccccc'
    , 'ddd', 'dddd', 'ddddd', 'dddddd'
    , 'eee', 'eeee', 'eeeee', 'eeeeee' ]

POSSIBLE_SIGNALS_AS_SET = set(POSSIBLE_SIGNALS)

app = Flask(__name__)

@app.route('/')
def home():
    real_ip_addr = request.environ.get('HTTP_X_REAL_IP', request.remote_addr)
    return rt( 'buttons.html'
             , button_pressed=session.get('BUTTON_PRESSED')
             , real_ip_addr=real_ip_addr )

@app.route('/processinput', methods=['POST'])
def processinput():
    buttons_pressed_as_list = list( request.form )
    if len(buttons_pressed_as_list) == 1:
        button_pressed = buttons_pressed_as_list[0]
        if button_pressed in POSSIBLE_SIGNALS_AS_SET:
            real_ip_addr = request.environ.get('HTTP_X_REAL_IP', request.remote_addr)
            # tstamp_str = dt.now().isoformat(' ')
            tstamp = round(t() * 1000)
            raw_payload = { 'button': button_pressed,
                            'tstamp': tstamp }
            payload = dumps(raw_payload)
            print(payload)
            session['BUTTON_PRESSED'] = button_pressed
            app.config['PRODUCER'].produce('raw', key=button_pressed.encode(), value=payload.encode())
            app.config['PRODUCER'].flush()
    return redirect('/')

if __name__ == '__main__':
    kafka_broker = getenv('KAFKA_BROKER', 'localhost:9092')
    app.config['PRODUCER'] = KPR({'bootstrap.servers': kafka_broker})
    app.config['SECRET_KEY'] = getenv('SECRET_SESSION_KEY', str(uuid1()))
    flask_port = int(getenv('FLASK_PORT'))
    http_server = WSGIServer(('', flask_port), app)
    http_server.serve_forever()
```
