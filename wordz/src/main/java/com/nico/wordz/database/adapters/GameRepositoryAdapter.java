package com.nico.wordz.database.adapters;

import com.nico.wordz.database.GameEntity;
import com.nico.wordz.database.PlayerEntity;
import com.nico.wordz.domain.Game;
import com.nico.wordz.domain.Player;
import com.nico.wordz.domain.Word;
import com.nico.wordz.domain.ports.out.GameRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Component
public class GameRepositoryAdapter implements GameRepository {

    private final GameJPARepository games;
    private final PlayerRepositoryAdapter players;

    public GameRepositoryAdapter(GameJPARepository games, PlayerRepositoryAdapter players) {
        this.games = requireNonNull(games);
        this.players = requireNonNull(players);
    }

    @Override
    @Transactional
    public void create(Game game) {
        requireNonNull(game);
        GameEntity gameEntity = fromGame(game);
        games.save(gameEntity);
    }

    @Override
    @Transactional
    public Optional<Game> findByPlayer(Player player) {
        if(player == null) {
            return Optional.empty();
        }
        return games.findAllByPlayerName(player.name())
                .stream()
                .map(this::toGame)
                .findAny();
    }

    @Override
    @Transactional
    public void update(Game game) {
        String playerName = game.player().name();
        Optional<Long> gameId = games.findAllByPlayerName(playerName)
                .stream()
                .findAny()
                .map(GameEntity::getId);

        GameEntity gameEntity = fromGame(game);
        gameId.ifPresent(gameEntity::setId);

        games.save(gameEntity);
    }

    private Game toGame(GameEntity gameEntity) {
        if(gameEntity == null) {
            return null;
        }
        Player gamePlayer = Optional.ofNullable(gameEntity.getPlayer())
                .map(PlayerEntity::toPlayer)
                .orElse(null);
        return new Game(new Word(gameEntity.getWord()), (int) gameEntity.getAttemptNumber(), gamePlayer, gameEntity.isGameOver(), Game.NO_SCORE);
    }

    private GameEntity fromGame(Game game) {
        if(game == null) {
            return null;
        }
        PlayerEntity playerEntity = findOrCreatePlayerEntityFor(game);
        return new GameEntity(game.word().text(), game.attemptNumber(), game.isGameOver(), playerEntity);
    }

    private PlayerEntity findOrCreatePlayerEntityFor(Game game) {
        Player player = requireNonNull(game.player());
        String playerName = player.name();
        if(players.findByName(playerName).isEmpty()) {
            players.create(playerName);
        }
        return players.findPlayerEntityByName(playerName).orElseThrow();
    }

    interface GameJPARepository extends JpaRepository<GameEntity, Long> {
        List<GameEntity> findAllByPlayerName(String playerName);
    }
}
