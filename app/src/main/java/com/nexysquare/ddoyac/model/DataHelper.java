package com.nexysquare.ddoyac.model;

import java.util.Collection;

import io.realm.Realm;

public class DataHelper {

    // Create 3 counters and insert them into random place of the list.
    public static void randomAddItemAsync(Realm realm) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for (int i = 0; i < 3; i++) {
//                    Item.create(realm, true);
                }
            }
        });
    }

    public static void addItemAsync(Realm realm) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
//                Item.create(realm);
            }
        });
    }

    public static void deleteItemAsync(Realm realm, final long id) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
//                Item.delete(realm, id);
            }
        });
    }

    public static void deleteItemsAsync(Realm realm, Collection<Integer> ids) {
        // Create an new array to avoid concurrency problem.
        final Integer[] idsToDelete = new Integer[ids.size()];
        ids.toArray(idsToDelete);
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for (Integer id : idsToDelete) {
//                    Item.delete(realm, id);
                }
            }
        });
    }
}
