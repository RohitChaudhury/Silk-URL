package Silk_Url.Silk_Url_Service.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import Silk_Url.Silk_Url_Service.Model.Entity.Users;

@Repository
public interface UserRepository extends JpaRepository<Users, Integer> {
    // Get User based on username
    @Query("SELECT u FROM Users u WHERE u.username = :username")
    public Users findUsersByUsername(String username);

    // Get User based on emaila
    @Query("SELECT u FROM Users u WHERE u.email = :email")
    public Users findUsersByEmail(String email);
}
