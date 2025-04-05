package pl.edu.uj.notes.note;

import jakarta.validation.constraints.NotBlank;

record DeleteNoteRequest(@NotBlank String id) {}
