module App.Subscriptions exposing (subscriptions)

import App.Messages exposing (..)
import App.Model exposing (..)
import Detail.Subscriptions
import Nav.Model exposing (Page(..))
import Ports
import Time exposing (second)


subscriptions : Model -> Sub Msg
subscriptions model =
    let
        refreshSub =
            Maybe.map (\_ -> Time.every (10 * second) (\_ -> GetSystemStatus)) model.appState.token |> Maybe.withDefault Sub.none

        subs =
            [ refreshSub
            , Ports.loginResult LoginResult
            ]
    in
    case model.page of
        Component _ _ _ _ ->
            let
                detailSub =
                    Sub.map DetailMsg <|
                        Detail.Subscriptions.subscriptions model.detail model.appState.url
            in
            Sub.batch <| detailSub :: subs

        _ ->
            Sub.batch subs
