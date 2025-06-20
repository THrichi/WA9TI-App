package com.application.wa9ti.controllers;

import com.application.wa9ti.dtos.*;
import com.application.wa9ti.enums.Role;
import com.application.wa9ti.models.*;
import com.application.wa9ti.repositories.ClientRepository;
import com.application.wa9ti.repositories.StoreServiceRepository;
import com.application.wa9ti.repositories.UserRepository;
import com.application.wa9ti.services.client.ClientServiceImp;
import com.application.wa9ti.services.employee.ImpEmployeeService;
import com.application.wa9ti.services.owner.OwnerServiceImp;
import com.application.wa9ti.services.user.UserService;
import com.application.wa9ti.services.user.UserServiceImp;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/public/joinUs")
public class JoinUsController {

    @Autowired
    private ImpEmployeeService employeeService;

    @Autowired
    private OwnerServiceImp ownerService;

    @Autowired
    private UserServiceImp userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ClientServiceImp clientServiceImp;

    @GetMapping
    public List<Owner> getAllOwners() {
        return ownerService.getAllOwners();
    }


    /*@PostMapping
    public ResponseEntity<Owner> createOwner(@RequestBody OwnerWithStoreDTO dto) {
        try {
            Owner createdOwner = ownerService.createOwnerWithStore(dto);
            return ResponseEntity.ok(createdOwner);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(null); // Vous pouvez personnaliser la réponse d'erreur
        }
    }*/


    @PostMapping("/create-employee")
    public ResponseEntity<?> createEmployee(@RequestBody NewUserDto userDto) {
        try {
            // Appeler le service pour créer l'utilisateur
            Employee employee = employeeService.createEmployee(userDto);
            if (employee != null) {
                return ResponseEntity.status(HttpStatus.CREATED).body("Employé créé avec succès.");
            }
            return ResponseEntity.status(HttpStatus.CREATED).body("Utilisateur créé avec succès.");
        } catch (IllegalArgumentException e) {
            // Gérer les erreurs spécifiques (exemple : doublon d'email)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            // Gérer les erreurs générales
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Une erreur est survenue.");
        }
    }


    @PostMapping("/create-client")
    public ResponseEntity<Void> createClient(@RequestBody NewUserDto userDto) {
       clientServiceImp.createClient(userDto);
       return ResponseEntity.noContent().build();
    }

    @PostMapping("/create-and-assign-employee")
    public ResponseEntity<Long> createAndAssignEmployee(@RequestBody EmployeeInviteCreateDto employeeDto) {
        return ResponseEntity.ok(employeeService.createEmployeeAndAssignToStore(employeeDto));
    }

    @PostMapping("/register")
    public UserDto registerOwner(@RequestBody OwnerRegistrationDto dto, HttpServletResponse response) {
        Owner owner = ownerService.createOwner(dto);
        LoginDto loginDto = new LoginDto(owner.getUser().getEmail(), dto.password());
        return this.userService.verifyUser(loginDto, response);
    }

    @GetMapping("/check-email/{email}/{phone}")
    public ResponseEntity<Boolean> checkEmailAndPhone(@PathVariable String email,@PathVariable String phone) {
        return ResponseEntity.ok(userService.checkEmailAndPhone(email,phone));
    }

}
