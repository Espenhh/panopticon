module App.Update exposing (update)

import App.Messages exposing (..)
import App.Model exposing (..)
import Components.Update
import Detail.Model
import Detail.Update
import Nav.Model exposing (..)
import Nav.Requests exposing (getDetails, getSystemStatus)
import Ports


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        UpdateUrl page ->
            updatePage page model

        GetSystemStatus ->
            ( model, getSystemStatus model.appState.url model.appState.token )

        SystemStatus (Err err) ->
            ( model
            , Ports.log ("Klarte ikke å dekode kallet mot /status: " ++ toString err)
            )

        SystemStatus (Ok components) ->
            ( { model | components = components }, Cmd.none )

        ComponentsMsg componentsMsg ->
            let
                componentsModel =
                    Components.Update.update componentsMsg model.components
            in
            ( { model | components = componentsModel }, Cmd.none )

        DetailMsg detailMsg ->
            let
                ( detailModel, cmd ) =
                    Detail.Update.update model.appState.token detailMsg model.detail

                mappedCmd =
                    Cmd.map DetailMsg cmd
            in
            ( { model | detail = detailModel }, mappedCmd )

        Login ->
            ( setToken Nothing model.appState |> setAppState model, Ports.login () )

        LoginResult token ->
            ( setToken (Just token) model.appState |> setAppState model
            , Cmd.none
            )


setToken : Maybe String -> AppState -> AppState
setToken token appState =
    { appState | token = token }


setAppState : Model -> AppState -> Model
setAppState model appState =
    { model | appState = appState }


updatePage : Page -> Model -> ( Model, Cmd Msg )
updatePage page m =
    let
        model =
            leftPage m.page m
    in
    case page of
        Components as page ->
            ( { model | page = page }, getSystemStatus model.appState.url model.appState.token )

        (Component env system component server) as page ->
            let
                cmd =
                    Cmd.map DetailMsg <| getDetails model.appState.url model.appState.token env system component server
            in
            ( { model | page = page }, cmd )


leftPage : Page -> Model -> Model
leftPage oldPage model =
    case oldPage of
        Component _ _ _ _ ->
            { model | detail = Detail.Model.init }

        _ ->
            model
