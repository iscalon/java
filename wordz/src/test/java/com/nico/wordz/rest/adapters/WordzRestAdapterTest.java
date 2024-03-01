package com.nico.wordz.rest.adapters;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tooling.JpaTest;

import java.util.HashMap;
import java.util.Map;

@JpaTest
@AutoConfigureMockMvc
class WordzRestAdapterTest {

  private static final String PLAYER_NAME = "Nico";
  private static final String START_GAME_URI = "/wordz/%s".formatted(PLAYER_NAME);
  private static final String ADD_WORD_URI = "/wordz/word";
  private static final String WORD_GUESS_URI = "/wordz/%s/guess".formatted(PLAYER_NAME);

  @Inject
  private MockMvc mockMvc;
  @Inject
  private ObjectMapper objectMapper;

  @Test
  @DisplayName("should start new game")
  void test01() throws Exception {
    this.mockMvc
            .perform(addWordPostRequest("DEVINE"));

    this.mockMvc
        .perform(post(START_GAME_URI))
        .andDo(print())
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("should not start new game if no word is available")
  void test02() throws Exception {
    this.mockMvc
            .perform(post(START_GAME_URI))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString("No available word to select")));
  }

  @Test
  @DisplayName("should not start new game when there is already a playing game")
  void test03() throws Exception {
    this.mockMvc
            .perform(addWordPostRequest("GUESSME"));

    this.mockMvc
            .perform(post(START_GAME_URI))
            .andDo(print())
            .andExpect(status().isNoContent());

    this.mockMvc
            .perform(post(START_GAME_URI))
            .andDo(print())
            .andExpect(status().isConflict())
            .andExpect(content().string(containsString("A game is already in play")));
  }

  @Test
  @DisplayName("should add new word")
  void test04() throws Exception {
    this.mockMvc
        .perform(addWordPostRequest("DEVINE"))
        .andDo(print())
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("should be able to make a guess")
  void test05() throws Exception {
    this.mockMvc
        .perform(addWordPostRequest("DEVINE"));
    this.mockMvc
            .perform(post(START_GAME_URI));

    this.mockMvc
            .perform(guessWordPostRequest("IRVINE"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.player").value("Nico"))
            .andExpect(jsonPath("$.score[0]").value("PARTIALLY_CORRECT"))
            .andExpect(jsonPath("$.score[1]").value("INCORRECT"))
            .andExpect(jsonPath("$.score[2]").value("CORRECT"))
            .andExpect(jsonPath("$.score[3]").value("CORRECT"))
            .andExpect(jsonPath("$.score[4]").value("CORRECT"))
            .andExpect(jsonPath("$.isGameOver").value(false));
  }

  @Test
  @DisplayName("game over on correct guess")
  void test06() throws Exception {
    this.mockMvc
        .perform(addWordPostRequest("DEVINE"));
    this.mockMvc
            .perform(post(START_GAME_URI));

    this.mockMvc
            .perform(guessWordPostRequest("IRVINE"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.player").value("Nico"))
            .andExpect(jsonPath("$.score[0]").value("PARTIALLY_CORRECT"))
            .andExpect(jsonPath("$.score[1]").value("INCORRECT"))
            .andExpect(jsonPath("$.score[2]").value("CORRECT"))
            .andExpect(jsonPath("$.score[3]").value("CORRECT"))
            .andExpect(jsonPath("$.score[4]").value("CORRECT"))
            .andExpect(jsonPath("$.isGameOver").value(false));

    this.mockMvc
            .perform(guessWordPostRequest("DEVINE"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.player").value("Nico"))
            .andExpect(jsonPath("$.score[0]").value("CORRECT"))
            .andExpect(jsonPath("$.score[1]").value("CORRECT"))
            .andExpect(jsonPath("$.score[2]").value("CORRECT"))
            .andExpect(jsonPath("$.score[3]").value("CORRECT"))
            .andExpect(jsonPath("$.score[4]").value("CORRECT"))
            .andExpect(jsonPath("$.isGameOver").value(true));
  }

  private MockHttpServletRequestBuilder addWordPostRequest(String word) throws JsonProcessingException {
    Map<String,Object> body = new HashMap<>();
    body.put("word", word);
    return post(ADD_WORD_URI)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body));
  }

  private MockHttpServletRequestBuilder guessWordPostRequest(String guess) throws JsonProcessingException {
    Map<String,Object> body = new HashMap<>();
    body.put("text", guess);
    return post(WORD_GUESS_URI)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body));
  }
}
