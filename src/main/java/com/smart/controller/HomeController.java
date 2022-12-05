package com.smart.controller;


import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
public class HomeController {

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/test")
    @ResponseBody
    public String test() {
       User user= new User();
       Contact contact = new Contact();

       user.getContacts().add(contact);
       user.setName("Arpan Mukherjee");
       user.setEmail("arpanchrome@gmail.com");
       userRepository.save(user);
       return "Working";
    }


    @RequestMapping("/")
    public  String  home (Model model) {
        model.addAttribute("title","Home -Smart Contact Manager");
        return "home";
    }

    @RequestMapping("/about")
    public String about (Model model) {
        model.addAttribute("title","About -Smart Contact Manager");
        return "about";
    }

    @RequestMapping("/signup")
    public String signup (Model model) {
        model.addAttribute("title","Register -Smart Contact Manager");
        model.addAttribute("user",new User());
        return "signup";
    }

    //handler for registering user
    @RequestMapping(value = "/do_register",method = RequestMethod.POST)
    public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result1,@RequestParam(value="agreement",defaultValue = "false") boolean agreement, Model model, HttpSession session)  {

       try {
           if(!agreement) {
               System.out.println("You haven't agreed the conditions");
               throw new Exception("You haven't agreed the conditions ");
           }

           if(result1.hasErrors()){
               System.out.println("ERROR"+result1.toString());
               model.addAttribute("user",user);
               return "signup";
           }

           user.setRole("ROLE_USER");
           user.setEnabled(true);
           user.setImageUrl("default.png");
           user.setPassword(passwordEncoder.encode(user.getPassword()));


           System.out.println("Agreement"+agreement);
           System.out.println("USER"+user);
           User result=this.userRepository.save(user);
           model.addAttribute("user",new User());
           session.setAttribute("message",new Message("Successfully registered !!","alert-success"));
           return "signup";
       }

       catch (Exception e) {
         e.printStackTrace();
         session.setAttribute("message",new Message("Something Went Wrong!!"+e.getMessage(),"alert-danger"));
         model.addAttribute("user",user);
         return "signup";
       }

    }


    //handler for custom login
    @GetMapping("/signin")
    public String  customLogin (Model model){
        model.addAttribute("title","Login Page");
        return "login";
    }



}
