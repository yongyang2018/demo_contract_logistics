const axios = require('axios');
const entry = 'http://localhost:8080';
const fromPubKey = '02f9d915954e04107d11fb9689a6330c22199e1e830857bff076e033bbca2888d4';
const contractAddr = 'e8a71957d03e72210275e0b5a18614861adfd3b1';
const toAddr = '54e670985631117904d10341676a57d11687bbc5';
const amount = 20;

const transaction = {
    version: 1634693120,
    type: 3,
    from: fromPubKey, // Sender pub key
    signature: 'ff',
    createdAt: Math.floor(Date.now() / 1000),
    nonce: 6,
    gasPrice: 100,
    to: contractAddr // Contract address
};

//function name and function args
transaction.payload = Buffer.concat(
    [
        Buffer.from(['transfer'.length]),
        Buffer.from('transfer', 'ascii'),
        Buffer.from(`{"to": "${toAddr}","amount": ${amount}}`, 'ascii')
    ]
);

transaction.payload = transaction.payload.toString('hex');

axios
    .get(`${entry}/rpc/account/${fromPubKey}`)
    .then(resp => resp.data)
    .then(data => {
        const body = [];
        transaction.nonce = data.data.nonce + 1;
          console.log('Nonce: '+transaction.nonce)
          body.push(JSON.parse(JSON.stringify(transaction)));
          transaction.nonce++;
        return axios.post(`${entry}/rpc/transaction`, body);
    })
    .then(() =>
  console.log('success'))
  .catch(console.error);
