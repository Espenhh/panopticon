module App.Subscriptions exposing (subscriptions)

import App.Model exposing (..)
import App.Messages exposing (..)
import Nav.Model exposing (Page(..))
import Detail.Subscriptions
import Time exposing (second)
import Auth exposing (loginResult)


subscriptions : Model -> Sub Msg
subscriptions model =
    let
        refreshSub =
            Maybe.map (\_ -> Time.every (10 * second) (\_ -> GetSystemStatus)) model.appState.token |> Maybe.withDefault Sub.none

        subs =
            [ refreshSub
            , loginResult LoginResult
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
