var mqtt    = require('mqtt');

var clients = []


for (var i=0; i <= 100; i++) {

  var client1  = mqtt.connect('mqtt://test.mosquitto.org');

  client1.on('connect', function () {
    client1.subscribe('#');
  });

  client1.on('message', function (topic, message) {
    publish(topic, message)
  });

  clients.push(client1);
}

////// 

var served = 0;
var total = 0;

var client2  = mqtt.connect('mqtt://localhost')

function publish(topic, message) {
  client2.publish(topic, message);

  served++;
  total++;
}

process.stdout.write("starting...");

setInterval(function() {
  process.stdout.clearLine();
  process.stdout.cursorTo(0);
  process.stdout.write("Total:" + total + ". " + served + "/sec");
  served = 0;
}, 1000); 
