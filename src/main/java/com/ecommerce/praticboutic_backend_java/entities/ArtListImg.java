package com.ecommerce.praticboutic_backend_java.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "artlistimg")
public class ArtListImg {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "artlistimgid")
    private Integer artListImgId;

    @Column(name = "customid")
    private Integer customId;

    @Column(name = "artid")
    private Integer artId;

    @Column(name = "image", length = 255)
    private String image;

    @Column(name = "favori")
    private Integer favori;

    @Column(name = "visible")
    private Integer visible;

    // Constructeurs
    public ArtListImg() {
    }

    public ArtListImg(Integer customId, Integer artId, String image, Integer favori, Integer visible) {
        this.customId = customId;
        this.artId = artId;
        this.image = image;
        this.favori = favori;
        this.visible = visible;
    }

    // Getters et Setters
    public Integer getArtListImgId() {
        return artListImgId;
    }

    public void setArtListImgId(Integer artListImgId) {
        this.artListImgId = artListImgId;
    }

    public Integer getCustomId() {
        return customId;
    }

    public void setCustomId(Integer customId) {
        this.customId = customId;
    }

    public Integer getArtId() {
        return artId;
    }

    public void setArtId(Integer artId) {
        this.artId = artId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Integer getFavori() {
        return favori;
    }

    public void setFavori(Integer favori) {
        this.favori = favori;
    }

    public Integer getVisible() {
        return visible;
    }

    public void setVisible(Integer visible) {
        this.visible = visible;
    }

    @Override
    public String toString() {
        return "ArtListImg{" +
                "artListImgId=" + artListImgId +
                ", customId=" + customId +
                ", artId=" + artId +
                ", image='" + image + '\'' +
                ", favori=" + favori +
                ", visible=" + visible +
                '}';
    }

    public List<Object> getDisplayData()
    {
        List<Object> row = new ArrayList<>();
        row.add(getArtListImgId());
        row.add(getCustomId());
        row.add(getImage());
        row.add(getFavori());
        row.add(getVisible());
        return row;
    }
}