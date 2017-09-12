package common.enums;

public enum RequestType {
    Channel_Get,
    Channel_join,
    Channel_leave,

    // When receive or send message
    Message,
    // Get friends list
    Friend_GetList,

    // when delete friend
    Friend_Delete,

    // add friend
    Friend_Add,

    // not impl
    Friend_SeeInfo,

    // create group
    Group_Create,

    // get user group list
    Group_List,

    // request to join group
    Group_RequestJoin,

    // return when Group_RequestJoin
    Group_AcceptJoin,

    // exit group
    Group_Exit,

    // disband group
    Group_Disband,

    // not impl
    Group_SetAdmin,

    // get group member list
    Group_MemberList,

    // not impl
    Group_EditInfo,

    // login request
    User_Login,

    // return when User_Login
    User_LoginResult,

    // user register
    User_Register,

    // not impl
    User_SeeInfo,

    // logout
    User_Logout,

    // not impl
    User_EditInfo,

    // only for network, heartbeat package
    Ping,
    Pong
}
