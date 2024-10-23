package uz.ns.cardprocessing.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

public enum CardStatus {
    BLOCKED,
    ACTIVE,
    CLOSED
}
