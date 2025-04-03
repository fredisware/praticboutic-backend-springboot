package com.ecommerce.praticboutic_backend_java.requests;

/**
 * Classe pour la requête de création d'une boutique
 */
public class BuildBouticRequest {
    private String deviceId;

    private String deviceType;

    /**
     * Retourne l'identifiant de l'appareil.
     * @return l'identifiant de l'appareil
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Définit l'identifiant de l'appareil.
     * @param deviceId le nouvel identifiant de l'appareil
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Retourne le type d'appareil.
     * @return le type d'appareil
     */
    public String getDeviceType() {
        return deviceType;
    }

    /**
     * Définit le type d'appareil.
     * @param deviceType le nouveau type d'appareil
     */
    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

}
