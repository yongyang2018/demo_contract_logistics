// 合约创建时打印一行信息
import {Arguments, Context, DB, log, Result, RLP, RLPItem, RLPList, RLPListReader} from "../lib";

const SUPPLIER = Uint8Array.wrap(String.UTF8.encode("supplier"));
const CONFIRM = Uint8Array.wrap(String.UTF8.encode("confirm"));

export function init(): void {
    log("financial contract deployed successfully");
}

export function reset(): void {
    DB.remove(SUPPLIER)
    DB.remove(CONFIRM)
}

// 读取供应商
export function getSupplier(): void {
    if (!DB.has(SUPPLIER)) {
        Result.write(RLPItem.NULL.getEncoded());
        return;
    }
    Result.write(DB.get(SUPPLIER));
}

// 保存供应商
export function saveSupplier(): void {
    const tx = Context.transaction();
    const header = Context.header();
    const args = new Arguments(tx.payload);
    const s = Supplier.fromEncoded(args.parameters);
    s.timestamp = header.createdAt;
    s.height = header.height;
    s.hash = tx.hash;
    s.address = tx.from;
    DB.set(SUPPLIER, s.getEncoded());
}

// 读取确认
export function getConfirm(): void {
    if (!DB.has(CONFIRM)) {
        Result.write(RLPItem.NULL.getEncoded());
        return;
    }
    Result.write(DB.get(CONFIRM));
}

// 核心企业保存
export function saveConfirm(): void {
    const tx = Context.transaction();
    const header = Context.header();
    const args = new Arguments(tx.payload);
    const c = Confirm.fromEncoded(args.parameters);
    c.timestamp = header.createdAt;
    c.height = header.height;
    c.hash = tx.hash;
    DB.set(CONFIRM, c.getEncoded());
}

// 供应商
class Supplier {
    constructor(
        // 链上的地址
        public address: Uint8Array,
        // 企业名称
        public supplierName: string,
        // 法人姓名
        public legalName: string,
        // 法人证件
        public legalId: string,
        // 融资金额
        public amount: u64,
        // 合同编号
        public number: string,
        // 上链时间戳
        public timestamp: u64,
        // 上链事务的哈希值
        public hash: Uint8Array,
        // 上链的高度
        public height: u64
    ) {
    }

    static fromEncoded(data: Uint8Array): Supplier {
        const li = RLPList.fromEncoded(data);
        const r = new RLPListReader(li);
        return new Supplier(
            r.bytes(), r.string(), r.string(),
            r.string(), r.u64(), r.string(),
            r.u64(), r.bytes(), r.u64()
        );
    }

    getEncoded(): Uint8Array {
        const elements = [
            RLP.encodeBytes(this.address),
            RLP.encodeString(this.supplierName),
            RLP.encodeString(this.legalName),
            RLP.encodeString(this.legalId),
            RLP.encodeU64(this.amount),
            RLP.encodeString(this.number),
            RLP.encodeU64(this.timestamp),
            RLP.encodeBytes(this.hash),
            RLP.encodeU64(this.height)
        ];

        return RLP.encodeElements(elements);
    }
}

class Confirm {
    constructor(
        // 事件戳
        public timestamp: u64,
        // 区块高度
        public height: u64,
        // 事务哈希
        public hash: Uint8Array
    ) {
    }

    static fromEncoded(data: Uint8Array): Confirm {
        const li = RLPList.fromEncoded(data);
        const r = new RLPListReader(li);
        return new Confirm(r.u64(), r.u64(), r.bytes());
    }

    getEncoded(): Uint8Array {
        const elements = [
            RLP.encodeU64(this.timestamp),
            RLP.encodeU64(this.height),
            RLP.encodeBytes(this.hash)
        ];
        return RLP.encodeElements(elements);
    }
}
