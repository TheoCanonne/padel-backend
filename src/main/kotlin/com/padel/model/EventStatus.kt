package com.padel.model

enum class EventStatus {
    OPEN,       // Ouvert aux candidatures
    FULL,       // Complet (capacité atteinte)
    CONFIRMED,  // Confirmé, en attente de l'événement
    COMPLETED,  // Terminé
    CANCELLED,  // Annulé
    POSTPONED   // Reporté
}
