package com.pogojava.pogojavaapi.pokegoapi.api.map.fort;

import com.google.protobuf.ProtocolStringList;

import java.util.List;

import POGOProtos.Data.Gym.GymMembershipOuterClass.GymMembership;
import POGOProtos.Map.Fort.FortDataOuterClass.FortData;
import POGOProtos.Networking.Responses.GetGymDetailsResponseOuterClass.GetGymDetailsResponse;

/**
 * Created by iGio90 on 05/09/16.
 */
public class GymDetails {
    private final FortData fortData;
    private final List<GymMembership> memberships;
    private final String name;
    private final ProtocolStringList urls;

    public GymDetails(GetGymDetailsResponse gymDetailsResponse) {
        this.fortData = gymDetailsResponse.getGymState().getFortData();
        this.memberships = gymDetailsResponse.getGymState().getMembershipsList();
        this.name = gymDetailsResponse.getName();
        this.urls = gymDetailsResponse.getUrlsList();
    }
}
