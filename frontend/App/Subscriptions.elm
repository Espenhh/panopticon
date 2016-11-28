module App.Subscriptions exposing (subscriptions)

import App.Model exposing (..)
import App.Messages exposing (..)
import Nav.Model exposing (Page(..))
import Detail.Subscriptions
import Time exposing (second)


subscriptions : Model -> Sub Msg
subscriptions model =
    let
        subs =
            [ Time.every (10 * second) (\_ -> GetSystemStatus) ]
    in
        case model.page of
            Component _ _ _ _ ->
                let
                    detailSub =
                        Sub.map DetailMsg <|
                            Detail.Subscriptions.subscriptions model.detail model.flags.url
                in
                    Sub.batch <| detailSub :: subs

            _ ->
                Sub.batch subs
