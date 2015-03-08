
sbt -Ddemo.restServer.port=7171 "test:run demo 1 1" &
sleep 10
sbt -Ddemo.restServer.port=7172 "test:run demo 1 2" &
sleep 10
sbt -Ddemo.restServer.port=7173 "test:run demo 1 3" &
sleep 10
sbt -Ddemo.restServer.port=7174 "test:run demo 1 4" &
sleep 10

sbt -Ddemo.restServer.port=7271 "test:run demo 2 1" &
sleep 10
sbt -Ddemo.restServer.port=7272 "test:run demo 2 2" &
sleep 10
sbt -Ddemo.restServer.port=7273 "test:run demo 2 3" &
sleep 10
sbt -Ddemo.restServer.port=7274 "test:run demo 2 4" &
sleep 10

sbt -Ddemo.restServer.port=7371 "test:run demo 3 1" &
sleep 10
sbt -Ddemo.restServer.port=7372 "test:run demo 3 2" &
sleep 10
sbt -Ddemo.restServer.port=7373 "test:run demo 3 3" &
sleep 10
sbt -Ddemo.restServer.port=7374 "test:run demo 3 4" &
sleep 10

sbt -Ddemo.restServer.port=7471 "test:run demo 4 1" &
sleep 10
sbt -Ddemo.restServer.port=7472 "test:run demo 4 2" &
sleep 10
sbt -Ddemo.restServer.port=7473 "test:run demo 4 3" &
sleep 10
sbt -Ddemo.restServer.port=7474 "test:run demo 4 4" &

