package com.smart.dao;

import com.smart.entities.Contact;
import com.smart.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ContactRepository extends JpaRepository<Contact,Integer> {

    //pagination
    //from Contact as c where c.user.id =:userId
    @Query("Select c from Contact as c where c.user.id =:userId")
    //current page  page amd records per page 5
    public Page<Contact> findContactsByUser(@Param("userId") int userId, Pageable pageable);



    // search
    public List<Contact> findByNameContainingAndUser(String keywords, User user);



}
