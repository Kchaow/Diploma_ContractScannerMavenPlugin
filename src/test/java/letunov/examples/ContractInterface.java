package letunov.examples;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface ContractInterface {
    @RequestMapping("/service/{param}")
    List<Person> getPerson(@PathVariable String param);

    @PostMapping("/service/{param}")
    ResponseEntity<Dog> postPerson(@PathVariable String param, @RequestBody List<Person> person);

    @GetMapping
    ResponseEntity<List<String>> getStrings();

    @GetMapping({"/catalog/items"})
    ResponseEntity<List<ItemDto>> getItems();
}
