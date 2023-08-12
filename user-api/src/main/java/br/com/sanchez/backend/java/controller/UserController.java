package br.com.sanchez.backend.java.controller;

import br.com.sanchez.backend.java.dto.UserDTO;
import br.com.sanchez.backend.java.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/users/health")
    public String health() {
        return "USER UP " + LocalDateTime.now();
    }

    @GetMapping("/users")
    public List<UserDTO> getUsers() {
        return userService.getAll();
    }

    @GetMapping("/users/{id}")
    UserDTO findById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @GetMapping("/users/cpf/{cpf}")
    UserDTO findByCpf(@RequestParam(name = "key", required = true) String key, @PathVariable String cpf) {
        return userService.findByCpf(cpf, key);
    }

    @PostMapping("/users")
    UserDTO newUser(@RequestBody UserDTO userDTO) {
        log.info("Inserindo usuario no banco {}", userDTO.getNome());
        return userService.save(userDTO);
    }

    @DeleteMapping("/users/{id}")
    UserDTO delete(@PathVariable Long id) {
        return userService.delete(id);
    }

    @GetMapping("/users/search")
    public List<UserDTO> queryByNome(@RequestParam(name = "nome", required = true) String nome) {
        return userService.queryByNome(nome);
    }

}