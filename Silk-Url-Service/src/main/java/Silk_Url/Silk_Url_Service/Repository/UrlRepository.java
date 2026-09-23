package Silk_Url.Silk_Url_Service.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import Silk_Url.Silk_Url_Service.Model.Entity.Urls;

@Repository
public interface UrlRepository extends JpaRepository<Urls, Integer> {
    @Query("SELECT u FROM Urls u ORDER BY u.id DESC LIMIT 1")
    public Urls getLastRowData();

    @Query("SELECT u FROM Urls u WHERE u.userId = :userId")
    public List<Urls> getAllUsersUrls(long userId);

    @Query("SELECT u FROM Urls u WHERE u.id = :id AND u.userId = :userId")
    public Urls findUrlByIdAndUser(long id, long userId);

    @Query("SELECT u FROM Urls u WHERE u.id = :id")
    public Urls findUrlById(long id);
}