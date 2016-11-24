module Components.Decoder exposing (decoder)

import Json.Decode exposing (Decoder, list, andThen, succeed)
import Components.Model exposing (..)
import Component.Decoder
import Component.Model


decoder : Decoder Model
decoder =
    andThen decodeModel <| list Component.Decoder.decoder


decodeModel : List Component.Model.Model -> Decoder Model
decodeModel =
    succeed << Model << List.map partitionEnv << groupWhile env << List.sortBy .environment


sort : Component.Model.Model -> String
sort s =
    s.environment


partitionEnv : List Component.Model.Model -> Environment
partitionEnv components =
    case List.head components of
        Just component ->
            Environment component.environment <|
                List.sortWith componentSorter components

        Nothing ->
            Environment "Unknown" components


componentSorter : Component.Model.Model -> Component.Model.Model -> Order
componentSorter a b =
    case compare a.component b.component of
        EQ ->
            compare a.server b.server

        a ->
            a


groupWhile : (a -> a -> Bool) -> List a -> List (List a)
groupWhile fn ls =
    case ls of
        [] ->
            []

        x :: xs ->
            let
                ( ys, zs ) =
                    span (fn x) xs
            in
                (x :: ys) :: groupWhile fn zs


span : (a -> Bool) -> List a -> ( List a, List a )
span p xs =
    ( takeWhile p xs, dropWhile p xs )


env : Component.Model.Model -> Component.Model.Model -> Bool
env a b =
    a.environment == b.environment


takeWhile : (a -> Bool) -> List a -> List a
takeWhile predicate list =
    case list of
        [] ->
            []

        x :: xs ->
            if (predicate x) then
                x :: takeWhile predicate xs
            else
                []


dropWhile : (a -> Bool) -> List a -> List a
dropWhile predicate list =
    case list of
        [] ->
            []

        x :: xs ->
            if (predicate x) then
                dropWhile predicate xs
            else
                list
