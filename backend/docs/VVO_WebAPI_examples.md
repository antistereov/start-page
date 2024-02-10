**POST** http://webapi.vvo-online.de/dm?format=json

Accept: application/json, text/plain, */*

```json
{
    "stopid": "33000037",
    "time": "2016-11-13T16:01:12Z",
    "isarrival": false,
    "limit": 30,
    "shorttermchanges": true,
    "mot": [
      "Tram",
      "CityBus",
      "IntercityBus",
      "SuburbanRailway",
      "Train",
      "Cableway",
      "Ferry",
      "HailedSharedTaxi"
    ]
}
```

**POST** https://webapi.vvo-online.de/tr/pointfinder?format=json

Accept: application/json, text/plain, */*

```json
{
    "limit": 0,
    "query": "postpl",
    "stopsOnly": false,
    "dvb": true
}
```

**POST** https://webapi.vvo-online.de/rc?format=json

Accept: application/json, text/plain, */*

```json
{
  "shortterm": true
}
```

**POST** https://webapi.vvo-online.de/tr/trips?format=json

Accept: application/json, text/plain, */*

```json
{
    "origin": "33000134",
    "destination": "33000013",
    "time": "2016-11-13T15:55:46.552Z",
    "isarrivaltime": false,
    "shorttermchanges": true,
    "mobilitySettings": {
        "mobilityRestriction": "None"
    },
    "standardSettings": {
        "maxChanges": "Unlimited",
        "walkingSpeed": "Normal",
        "footpathToStop": 5,
        "mot": [
            "Tram",
            "CityBus",
            "IntercityBus",
            "SuburbanRailway",
            "Train",
            "Cableway",
            "Ferry",
            "HailedSharedTaxi"
        ],
        "includeAlternativeStops": true
    }
}
```

POST https://webapi.vvo-online.de/tr/handyticket?format=json

Accept: application/json, text/plain, */*

```json
{
    "origin": {
    "id": "33000134",
    "name": "Dresden Münchner Platz",
    "farezone": 10
    },
    "destination": {
    "id": "33000013",
    "name": "Dresden Albertplatz",
    "farezone": 10
    },
    "priceLevel": 1
}
```