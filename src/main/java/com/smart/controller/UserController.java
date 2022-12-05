package com.smart.controller;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import com.razorpay.*;

@Controller
@RequestMapping("/user")

public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;

     // method for adding common data to response
    @ModelAttribute
    public void addCommonData (Model model,Principal principal) {
        String username=principal.getName();
        User user =this.userRepository.getUserByUserName(username);
        model.addAttribute("user",user);

    }
    //Dashboard home
    @RequestMapping("/index")
    public String dashboard(Model model, Principal principal) {

        model.addAttribute("title","User Dashboard");
        return "normal/user_dashboard";
    }


    //open add form handler

    @GetMapping("/add-contact")
    public String openAddContactForm(Model model) {


        model.addAttribute("title","Add Contact");
        model.addAttribute("contact",new Contact());
        return "normal/add_contact_form";
    }
    //processing add contact form
    @PostMapping("/process-contact")
    public String processContact(
            @ModelAttribute Contact contact,
            @RequestParam(name = "profileImage") MultipartFile file,
            Principal principal,HttpSession session) throws  Exception {

        try {
            String name = principal.getName();
            User user = this.userRepository.getUserByUserName(name);



             //Processing file

            if(file.isEmpty())
            {
                //if file is empty try our message
                System.out.println("File is empty");
                contact.setImage("contact.png");
            }
            else  {
                //upload the file to folder and update the name to contactt
                contact.setImage(file.getOriginalFilename());

             File saveFile= new ClassPathResource("static/img").getFile();
            // Path path= Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
             String uri=saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename();
             Path path=Paths.get(uri);

             Files.copy(file.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
             System.out.println("Image is uploaded"+path);



            }

            user.getContacts().add(contact);
            contact.setUser(user);
            this.userRepository.save(user);

            System.out.println("DATA" + contact);
            System.out.println("Added to database");

            // message success
            session.setAttribute("message",new Message("Your Contact is Added!! Please Add more","success"));

        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            // message error
            session.setAttribute("message",new Message("Somethong went wrong try again","danger"));
        }

        return "normal/add_contact_form";
    }


    //show contacts handler


    @GetMapping("/show-contacts/{page}")

    public String showContacts (@PathVariable("page") Integer page,Model m,Principal principal) {

        String username=principal.getName();
        User user =this.userRepository.getUserByUserName(username);
        m.addAttribute("title","Show user Contacts");
        Pageable pageable= PageRequest.of(page,5);
        Page<Contact> contacts=this.contactRepository.findContactsByUser(user.getId(),pageable);
        m.addAttribute("contacts",contacts);
        m.addAttribute("currentPage",page);
        m.addAttribute("totalPages",contacts.getTotalPages());
        return "normal/show_contacts";

    }


    // showing particular contact details

    @RequestMapping("/{cId}/contact")
    public String showContactDetail (@PathVariable("cId") Integer cId,Model model,Principal principal) {
        System.out.println("CID"+cId);

        String username=principal.getName();
        User user =this.userRepository.getUserByUserName(username);

        Optional<Contact> contactOptional= this.contactRepository.findById(cId);
        Contact contact= contactOptional.get();
        if(user.getId()==contact.getUser().getId()) {
            model.addAttribute("contact", contact);
            model.addAttribute("title",contact.getName());
        }


        return "normal/contact_detail";
    }


    @GetMapping("/delete/{cId}")

    public String deleteContact (@PathVariable("cId")  Integer cId,Model m,HttpSession session,Principal principal) {

        try {
            Optional<Contact> contactOptional = this.contactRepository.findById(cId);

            String username = principal.getName();
            User user = this.userRepository.getUserByUserName(username);
            Contact contact = contactOptional.get();

            if (user.getId() == contact.getUser().getId()) {
                File deleteFile=new ClassPathResource("static/img").getFile();
                File file1=new File(deleteFile,contact.getImage());
                if(file1.exists()) {
                    file1.delete();
                }


                user.getContacts().remove(contact);
                this.userRepository.save(user);
                // unlinking contact from user that is  many to  one mapping
                //contact.setUser(null);
             //   this.contactRepository.delete(contact);
                session.setAttribute("message", new Message("Contact deleted successfully", "success"));

            }
        }
        catch (Exception e ){
              session.setAttribute("message",new Message("Some error occurred","danger"));
            System.out.println(e.getMessage());
        }


        return "redirect:/user/show-contacts/0";
    }


    //open-update form handler


    @PostMapping("/update-contact/{cid}")
    public  String updateForm (@PathVariable("cid") Integer cid, Model m) {
        m.addAttribute("title","Update Contact");
        Contact contact= this.contactRepository.findById(cid).get();
        m.addAttribute("contact",contact);
        return "normal/update_form";
    }


    // update contact handler

    @RequestMapping(value = "/process-update",method = RequestMethod.POST)
    public String updateHandler (@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,Model m,HttpSession session, Principal principal) {


        try {

            Contact oldcontactDetail= this.contactRepository.findById(contact.getcId()).get();

            if(!file.isEmpty()){

                // delete new photo
                File deleteFile=new ClassPathResource("static/img").getFile();
                File file1=new File(deleteFile,oldcontactDetail.getImage());
                file1.delete();

                //update new photo
                File saveFile=new ClassPathResource("static/img").getFile();
                Path path= Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
                Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
                contact.setImage(file.getOriginalFilename());
            }

            else {
                contact.setImage(oldcontactDetail.getImage());
            }

            User user =this.userRepository.getUserByUserName(principal.getName());
            contact.setUser(user);
            this.contactRepository.save(contact);
            session.setAttribute("message",new Message("Your contact is updated ","success"));

        }

        catch (Exception e) {
              e.printStackTrace();
        }
        System.out.println("Contact Name"+contact.getName());
        System.out.println("Contact Id"+contact.getcId());

        return "redirect:/user/" +contact.getcId() +"/contact";
    }




    @GetMapping("/profile")
    public  String yourProfile (Model model) {
        model.addAttribute("title","Profile Page");
        return "normal/profile";
    }



    @GetMapping("/settings")
    public String openSettings() {

        return "normal/settings";

    }


    @PostMapping("/create_order")
    @ResponseBody
    public String createOrder (@RequestBody Map<String,Object> data) throws Exception {

            //System.out.println("Hey order function exe");

            System.out.println(data);
            int amt = Integer.parseInt(data.get("amount").toString());
            var client = new RazorpayClient("rzp_test_skQpbDuLxLeXrK", "QIXJ5xFjnD4toD1lIiqLVXKE");

            JSONObject ob= new JSONObject();
            ob.put("amount",amt*100);
            ob.put("currency","INR");
            ob.put("receipt","txn_235425");

            //Creating new order

           Order order= client.orders.create(ob);

           System.out.println(order);


            return order.toString();


    }













}
