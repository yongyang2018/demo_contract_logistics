package com.yongyang.contract;

import lombok.Setter;
import org.tdf.common.store.Store;
import org.tdf.common.trie.Trie;
import org.tdf.common.util.HexBytes;
import org.tdf.rlp.RLPCodec;
import org.tdf.sunflower.state.Account;
import org.tdf.sunflower.state.PreBuiltContract;
import org.tdf.sunflower.state.StateTrie;
import org.tdf.sunflower.types.Header;
import org.tdf.sunflower.types.Transaction;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Setter
public class ExpressContract implements PreBuiltContract {
    public static final HexBytes CONTRACT_ADDRESS = HexBytes.fromHex("000000000000000000000000000000000000000e");

    static final byte[] SENDER = "sender".getBytes(StandardCharsets.US_ASCII);
    static final byte[] RECEIVER = "receiver".getBytes(StandardCharsets.US_ASCII);
    static final byte[] ORDER = "order".getBytes(StandardCharsets.US_ASCII);


    private StateTrie<HexBytes, Account> accountTrie;
    private Trie<byte[], byte[]> contractStorageTrie;

    @Override
    public Account getGenesisAccount() {
        return Account.emptyContract(CONTRACT_ADDRESS);
    }

    @Override
    public void update(Header header, Transaction transaction, Map<HexBytes, Account> accounts, Store<byte[], byte[]> contractStorage) {
        HexBytes payload = transaction.getPayload();
        int type = payload.get(0);
        switch (type){
            // 保存寄件人
            case 0:{
                contractStorage.put(SENDER, payload.slice(1).getBytes());
                break;
            }
            // 创建单据
            case 1:{
                Order o = RLPCodec.decode(payload.slice(1).getBytes(), Order.class);
                o.setHeight(header.getHeight());
                o.setTransactionHash(transaction.getHash());
                contractStorage.put(ORDER, RLPCodec.encode(o));
                break;
            }
            // 添加物流时间戳信息
            case 2:{
                OrderPayload p =
                        RLPCodec.decode(payload.slice(1).getBytes(), OrderPayload.class);
                Order o = RLPCodec.decode(contractStorage.get(ORDER).get(), Order.class);
                o.getTimestamps().add(p.getTimestamp());
                o.getDescription().add(p.getDescription());
                contractStorage.put(ORDER, RLPCodec.encode(o));
                break;
            }
            // 重置
            case 3:{
                contractStorage.remove(SENDER);
                contractStorage.remove(ORDER);
                break;
            }
        }
    }

    @Override
    public Map<byte[], byte[]> getGenesisStorage() {
        return Collections.emptyMap();
    }

    private Store<byte[], byte[]> getStore(byte[] stateRoot){
        Account a = accountTrie.getTrie().revert(stateRoot)
                .get(CONTRACT_ADDRESS).get();
        return
                contractStorageTrie.revert(a.getStorageRoot());
    }

    public Optional<Sender> getSender(byte[] stateRoot){
        Store<byte[], byte[]> store = getStore(stateRoot);
        return store.get(SENDER)
                .map(x -> RLPCodec.decode(x, Sender.class));
    }

    public Optional<Order> getOrder(byte[] stateRoot){
        Store<byte[], byte[]> store = getStore(stateRoot);
        return store.get(ORDER)
                .map(x -> RLPCodec.decode(x, Order.class));
    }
}
