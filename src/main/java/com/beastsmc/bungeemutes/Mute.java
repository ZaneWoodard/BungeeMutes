package com.beastsmc.bungeemutes;

import java.util.Date;

public class Mute {

    private String mutedUUID;
    private String muterName;
    private Date expiration;
    private String reason;

    public Mute(String mutedUUID, String muterName, Date expiration, String reason) {
        this.mutedUUID = mutedUUID;
        this.muterName = muterName;
        this.expiration = expiration;
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getMutedUUID() {
        return mutedUUID;
    }

    public void setMutedUUID(String mutedUUID) {
        this.mutedUUID = mutedUUID;
    }

    public String getMuterName() {
        return muterName;
    }

    public void setMutedName(String muterName) {
        this.muterName = muterName;
    }

    public Date getExpiration() {
        return this.expiration;
    }

    public void setExpiration(Date expiration) {
        this.expiration = expiration;
    }

    public boolean isExpired() {
        return !isPermanent() && expiration.before(new Date());
    }

    public boolean isPermanent() {
        return expiration==null;
    }

    public void remove() {
        BungeeMutes.instance.storage.deleteMute(this);
        BungeeMutes.instance.syncer.sendUnmute(this);
    }

    public String toString() {
        String printableReason = reason;
        if(reason==null) printableReason = "";
        return String.format("target=%s;issuer=%s;expiration=%s;reason=%s",
                             mutedUUID,
                             muterName,
                             (expiration==null) ? "never" : expiration.toString(),
                             printableReason);
    }
}
