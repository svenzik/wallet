package com.playtech.wallet.spring.controller;

import com.playtech.wallet.domain.*;
import com.playtech.wallet.domain.messages.PlayerMessage;
import com.playtech.wallet.repository.PlayerRepository;
import com.playtech.wallet.repository.exceptions.PlayerNotFoundException;
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

    protected PlayerController() {
    }

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
        Player repositoryPlayer = null;

        try {
            repositoryPlayer = playerRepository.findByUsername(playerMessage.getUsername());
        } catch (PlayerNotFoundException e) {
            repositoryPlayer = PlayerFactory.createNewPlayer(playerMessage.getUsername());
            repositoryPlayer = playerRepository.save(repositoryPlayer);
        }

        repositoryPlayer.changeBalance(BigDecimal.ZERO.subtract(repositoryPlayer.getBalance()));
        repositoryPlayer = playerRepository.save(repositoryPlayer);

        return wrap(repositoryPlayer);
    }

    @RequestMapping(value = "/{username}", method = RequestMethod.GET, produces = {"application/json", "application/xml"})
    public HttpEntity<Player> get(@PathVariable("username") String username) throws PlayerNotFoundException {
        return wrap(playerRepository.findByUsername(username));
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
