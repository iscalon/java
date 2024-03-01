package com.nico.wordz.database.adapters;

import com.nico.wordz.database.PlayerEntity;
import com.nico.wordz.domain.Player;
import com.nico.wordz.domain.ports.out.PlayerRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Component
public class PlayerRepositoryAdapter implements PlayerRepository {

    private final PlayerJPARepository players;

    public PlayerRepositoryAdapter(PlayerJPARepository players) {
        this.players = requireNonNull(players);
    }

    @Override
    public Player create(String name) {
        Player player = new Player(name);
        players.save(PlayerEntity.fromPlayer(player));
        return player;
    }

    @Override
    @Transactional
    public Optional<Player> findByName(String name) {
        return players.findFirstByName(name)
                .map(PlayerEntity::toPlayer);
    }

    public Optional<PlayerEntity> findPlayerEntityByName(String name) {
        return players.findFirstByName(name);
    }

    interface PlayerJPARepository extends JpaRepository<PlayerEntity, Long> {
        Optional<PlayerEntity> findFirstByName(String name);
    }
}
