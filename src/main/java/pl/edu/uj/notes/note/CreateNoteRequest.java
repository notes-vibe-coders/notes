package pl.edu.uj.notes.note;

import jakarta.validation.constraints.NotBlank;

record CreateNoteRequest(@NotBlank String title, @NotBlank String content, String password) {}
