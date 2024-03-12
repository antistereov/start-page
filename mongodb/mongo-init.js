var authDb = db.getSiblingDB('admin');

authDb.createUser({
    user: process.env.MONGO_READ_USERNAME,
    pwd: process.env.MONGO_READ_PASSWORD,
    roles: [
        { role: 'read', db: 'start' },
    ],
});

authDb.createUser({
    user: process.env.MONGO_READWRITE_USERNAME,
    pwd: process.env.MONGO_READWRITE_PASSWORD,
    roles: [
        { role: 'readWrite', db: 'start', },
    ],
});

var startDb = db.getSiblingDB('start');
startDb.createCollection("users");
