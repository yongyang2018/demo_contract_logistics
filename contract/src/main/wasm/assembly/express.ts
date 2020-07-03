import {Arguments, Context, DB, Hex, JSONBuilder, log, Result, RLP, RLPItem, RLPList, RLPListReader} from "../lib";

const SENDER = Uint8Array.wrap(String.UTF8.encode("sender"));
const ORDER = Uint8Array.wrap(String.UTF8.encode("order"));
const PATCH = Uint8Array.wrap(String.UTF8.encode("patch"));

// 寄件人
class Sender {
    constructor(
        // 地址
        public address: Uint8Array,
        // 邮寄物品
        public type: string,
        // 真实姓名
        public name: string,
        // 身份证号
        public id: string,
        // 电话号码
        public phone: string,
        // 简单说明
        public description: string,
        // 上链高度
        public height: u64,
        // 上链的事务哈希
        public hash: Uint8Array,
    ) {
    }

    // 从 rlp 解码
    static fromEncoded(data: Uint8Array): Sender {
        const li = RLPList.fromEncoded(data);
        const r = new RLPListReader(li);
        return new Sender(
            r.bytes(), r.string(), r.string(),
            r.string(), r.string(), r.string(),
            r.u64(), r.bytes()
        )
    }

    // rlp 编码
    getEncoded(): Uint8Array {
        const elements = [
            RLP.encodeBytes(this.address),
            RLP.encodeString(this.type),
            RLP.encodeString(this.name),
            RLP.encodeString(this.id),
            RLP.encodeString(this.phone),
            RLP.encodeString(this.description),
            RLP.encodeU64(this.height),
            RLP.encodeBytes(this.hash)
        ];
        return RLP.encodeElements(elements);
    }
}

// 快递单
class Order {
    constructor(
        // 寄件人的地址
        public senderAddress: Uint8Array,
        // 订单号
        public id: string,
        // 寄件始发地
        public from: string,
        // 寄件目的地
        public to: string,
        // 上链高度
        public height: u64,
        // 上链的事务哈希
        public hash: Uint8Array,
        // 事件时间戳
        public timestamps: Array<u64>,
        // 事件描述
        public descriptions: Array<string>
    ) {
    }

    // 解析 rlp 编码
    static fromEncoded(data: Uint8Array): Order {
        const li = RLPList.fromEncoded(data);
        const r = new RLPListReader(li);
        const ret = new Order(
            r.bytes(), r.string(), r.string(),
            r.string(), r.u64(), r.bytes(),
            [], []
        );
        let r0 = r.reader();
        while (r0.hasNext()) {
            ret.timestamps.push(r0.u64());
        }
        r0 = r.reader();
        while (r0.hasNext()) {
            ret.descriptions.push(r0.string());
        }
        return ret;
    }

    // rlp 编码
    getEncoded(): Uint8Array {
        const timestamps: Array<Uint8Array> = [];
        for (let i = 0; i < this.timestamps.length; i++) {
            timestamps.push(RLP.encodeU64(this.timestamps[i]));
        }
        const descriptions: Array<Uint8Array> = [];
        for (let i = 0; i < this.descriptions.length; i++) {
            descriptions.push(RLP.encodeString(this.descriptions[i]));
        }
        const elements = [
            RLP.encodeBytes(this.senderAddress),
            RLP.encodeString(this.id),
            RLP.encodeString(this.from),
            RLP.encodeString(this.to),
            RLP.encodeU64(this.height),
            RLP.encodeBytes(this.hash),
            RLP.encodeElements(timestamps),
            RLP.encodeElements(descriptions)
        ];

        return RLP.encodeElements(elements);
    }
}

// 合约创建时打印一行信息
export function init(): void {
    log("express contract deployed successfully")
}

// 显示合约地址，调试用
export function address(): void{
    const ctx = Context.contract();
    JSONBuilder.putString("address", Hex.encode(ctx.address));
    Result.write(Uint8Array.wrap(String.UTF8.encode(JSONBuilder.build())));
}

// 存储订单
export function saveOrder(): void{
    const tx = Context.transaction();
    const header = Context.header();
    const args = new Arguments(tx.payload);
    const o = Order.fromEncoded(args.parameters);
    o.senderAddress = tx.from;
    o.height = header.height;
    o.hash = tx.hash;
    DB.set(ORDER, o.getEncoded());
}

// 读取订单
export function getOrder(): void{
    if(!DB.has(ORDER)){
        Result.write(RLPItem.NULL.getEncoded())
        return;
    }
    Result.write(DB.get(ORDER))
}

// 存储寄件人
export function saveSender(): void{
    const tx = Context.transaction();
    const header = Context.header();
    const args = new Arguments(tx.payload);
    const sender = Sender.fromEncoded(args.parameters);
    sender.hash = tx.hash;
    sender.height = header.height;
    sender.address = tx.from
    DB.set(SENDER, sender.getEncoded());
}

// 读取寄件人
export function getSender(): void{
    if(!DB.has(SENDER)){
        Result.write(RLPItem.NULL.getEncoded())
        return;
    }
    Result.write(DB.get(SENDER))
}


// 重置
export function reset(): void{
    DB.remove(SENDER)
    DB.remove(ORDER)
    DB.remove(PATCH)
}

// 获取物流上链信息
export function getPatch(): void{
    if(!DB.has(PATCH)){
        Result.write(RLPItem.NULL.getEncoded())
        return;
    }
    Result.write(DB.get(PATCH))
}

// 补充物流上链信息
export function modifyOrder(): void{
    assert(DB.has(ORDER), 'db has order')
    const tx = Context.transaction();
    const height = Context.header().height;
    const args = new Arguments(tx.payload);
    const o = Order.fromEncoded(args.parameters);
    const o0 = Order.fromEncoded(DB.get(ORDER));
    o0.timestamps = o.timestamps;
    o0.descriptions = o.descriptions;
    DB.set(ORDER, o0.getEncoded());
    o0.hash = tx.hash;
    o0.height = height;
    DB.set(PATCH, o0.getEncoded());
}
