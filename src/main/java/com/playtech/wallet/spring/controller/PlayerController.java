package com.playtech.wallet.spring.controller;

import com.playtech.wallet.domain.*;
import com.playtech.wallet.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/player")
public class PlayerController {

    @Autowired
    public PlayerController(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    private PlayerRepository playerRepository;

    @RequestMapping(value = "", method = RequestMethod.GET, produces = {"application/json", "application/xml"})
    public HttpEntity<List<Player>> getPlayers() {
        return wrap(playerRepository.findAll());
    }

    @RequestMapping(value = "", method = RequestMethod.POST, produces = {"application/json", "application/xml"})
    public HttpEntity<Player> createPlayer(@RequestBody PlayerMessage playerMessage) {

        //TODO: move to service
        Player repositoryPlayer = playerRepository.findByUsername(playerMessage.getUsername());

        if (repositoryPlayer == null) {
            repositoryPlayer = PlayerFactory.createNewPlayer(playerMessage.getUsername());
            repositoryPlayer = playerRepository.save(repositoryPlayer);
        } else {
//            throw new IllegalArgumentException("User allready exists in repository");
            //zero money
            repositoryPlayer.changeBalance(BigDecimal.ZERO.subtract(repositoryPlayer.getBalance()));
            repositoryPlayer = playerRepository.save(repositoryPlayer);
        }

        return wrap(repositoryPlayer);
    }

    @RequestMapping(value = "/{username}", method = RequestMethod.GET, produces = {"application/json", "application/xml"})
    public HttpEntity<Player> get(@PathVariable("username") String username) {
        return wrap(playerRepository.findByUsername(username));
//        return new ResponseEntity<Player>(playerRepository.findByUsername(username), HttpStatus.OK);
    }


    /**
     * For pot wrapping purpose
     * @param object data
     * @param <T> Domain object
     * @return REST entity
     */
    public <T> HttpEntity<T> wrap(T object) {
        if(object == null) {
            return new ResponseEntity<T>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<T>(object, HttpStatus.OK);
    }

//    public <T> HttpEntity<Collection<T>> wrap(Collection<T> object) {
//        if(object == null) {
//            return new ResponseEntity<Collection<T>>(HttpStatus.NOT_FOUND);
//        }
//        if (object.size() == 0) {
//            return new ResponseEntity<Collection<T>>(HttpStatus.NOT_FOUND);
//        }
//        return new ResponseEntity<Collection<T>>(object, HttpStatus.OK);
//    }
}
