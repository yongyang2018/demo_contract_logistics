import {Arguments, Context, DB, log, Result, RLP, RLPItem, RLPList, RLPListReader} from '../lib'

export function init(): void{
    log('公益合约已部署');
}

// 捐赠人
const DONOR = Uint8Array.wrap(String.UTF8.encode("donor"));

// 二次确认
const CONFIRM = Uint8Array.wrap(String.UTF8.encode("confirm"));


// 捐赠人包含的字段
class Donor{
    constructor(
        // 链上分配的地址
        public chainAddress: Uint8Array,
        // 捐赠姓名
        public name: string,
        // 捐赠内容
        public content: string,
        // 捐赠数量
        public quantity: u64,
        // 简介信息
        public info: string,
        // 捐赠地址
        public address: string,
        // 受益人
        public get: string,
        // 捐赠机构
        public donor: string,
        // 上链高度
        public height: u64,
        // 上链事务的哈希
        public hash: Uint8Array,
        // 上链的时间戳
        public timestamp: u64
    ) {
    }

    // 从 rlp 解码
    static fromEncoded(data: Uint8Array): Donor {
        const li = RLPList.fromEncoded(data);
        const r = new RLPListReader(li);
        return new Donor(
            r.bytes(),
            r.string(), r.string(), r.u64(),
            r.string(), r.string(), r.string(),
            r.string(), r.u64(), r.bytes(), r.u64()
        );
    }

    // rlp 编码
    getEncoded(): Uint8Array {
        const elements = [
            RLP.encodeBytes(this.chainAddress),
            RLP.encodeString(this.name),
            RLP.encodeString(this.content),
            RLP.encodeU64(this.quantity),
            RLP.encodeString(this.info),
            RLP.encodeString(this.address),
            RLP.encodeString(this.get),
            RLP.encodeString(this.donor),
            RLP.encodeU64(this.height),
            RLP.encodeBytes(this.hash),
            RLP.encodeU64(this.timestamp)
        ];
        return RLP.encodeElements(elements);
    }
}

class Confirm{
    constructor(
        // 捐赠说明
        public description: string,
        // 事件戳
        public timestamp: u64,
        // 区块高度
        public height: u64,
        // 事务哈希
        public hash: Uint8Array
    ) {
    }

    static fromEncoded(data: Uint8Array): Confirm{
        const li = RLPList.fromEncoded(data);
        const r = new RLPListReader(li);
        return new Confirm(r.string(), r.u64(), r.u64(), r.bytes());
    }

    getEncoded(): Uint8Array{
        const elements = [
            RLP.encodeString(this.description),
            RLP.encodeU64(this.timestamp),
            RLP.encodeU64(this.height),
            RLP.encodeBytes(this.hash)
        ];
        return RLP.encodeElements(elements);
    }
}

// 保存捐赠人
export function saveDonor(): void{
    const tx = Context.transaction();
    const header = Context.header();
    const args = new Arguments(tx.payload);
    const d = Donor.fromEncoded(args.parameters);
    d.height = header.height;
    d.hash = tx.hash;
    d.chainAddress = tx.from;
    d.timestamp = header.createdAt
    DB.set(DONOR, d.getEncoded());
}

// 保存二次确认信息
export function saveConfirm(): void{
    const tx = Context.transaction();
    const header = Context.header();
    const args = new Arguments(tx.payload);
    const c = Confirm.fromEncoded(args.parameters);
    c.height = header.height;
    c.timestamp = header.createdAt;
    c.hash = tx.hash
    DB.set(CONFIRM, c.getEncoded());
}

// 读取捐赠人
export function getDonor(): void{
    if(!DB.has(DONOR)){
        Result.write(RLPItem.NULL.getEncoded())
        return
    }
    Result.write(DB.get(DONOR))
}

// 读取二次确认信息
export function getConfirm(): void{
    if(!DB.has(CONFIRM)){
        Result.write(RLPItem.NULL.getEncoded())
        return
    }
    Result.write(DB.get(CONFIRM))
}

// 重置
export function reset(): void{
    DB.remove(DONOR)
    DB.remove(CONFIRM)
}
