package dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import models.Bill;

@Dao
public interface BillDAO {

    @Query("SELECT * FROM bill WHERE id IN (:billId) LIMIT 1")
    Bill getBillById(int billId);

    @Query("SELECT * FROM bill WHERE recipientId IN (:recipientId)")
    List<Bill> getBillsByRecipientId(int recipientId);

    @Query("SELECT * FROM bill WHERE senderId IN (:senderId)")
    List<Bill> getBillsBySenderId(int senderId);

    @Insert
    void insert(Bill bill);

    @Update
    void update(Bill bill);

    @Delete
    void delete(Bill bull);
}
