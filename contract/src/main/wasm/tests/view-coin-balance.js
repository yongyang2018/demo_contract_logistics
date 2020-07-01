const axios = require('axios');

const parameters = Buffer.concat(
    [
        Buffer.from(['getBalance'.length]),
        Buffer.from('getBalance', 'ascii'),
        Buffer.from(`{"address": "54e670985631117904d10341676a57d11687bbc5"}`, 'ascii')
    ]
);

axios
    .get('http://localhost:8080/rpc/contract/e8a71957d03e72210275e0b5a18614861adfd3b1?parameters=' + parameters.toString('hex'))
    .then(resp => resp.data)
    .then(x => {
        console.log(x);
        return x;
    })
    .then(x => Buffer.from(x.data, 'hex'))
    .then(b => JSON.parse(b.toString('ascii')))
    .then(console.log)
    .catch(console.error)
