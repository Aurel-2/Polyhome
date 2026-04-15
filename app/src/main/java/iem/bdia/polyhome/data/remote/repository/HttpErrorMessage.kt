package iem.bdia.polyhome.data.remote.repository

data class HttpErrorMessage(
    val badRequest: String = "Requête invalide",
    val unauthorized: String = "Non autorisé",
    val forbidden: String = "Accès refusé",
    val notFound: String = "Ressource introuvable",
    val conflict: String = "Conflit : la ressource existe déjà",
    val serverError: String = "Erreur serveur, réessayez plus tard",
    val noInternet: String = "Pas de connexion internet",
    val timeout: String = "Délai dépassé, réessayez",
    val unknown: String = "Erreur inconnue"
)
