package com.glennsyj.rivals.api.riot.model;

public record RiotAccountResponse(
    String puuid,
    String gameName,
    String tagLine,
    String id
) {
}
