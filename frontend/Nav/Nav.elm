module Nav.Nav exposing (..)

import Navigation
import UrlParser exposing (Parser, (</>), oneOf, map, s, string, parseHash, top)
import Nav.Model exposing (Page(..))
import String exposing (join)


toHash : Page -> String
toHash page =
    case page of
        Components ->
            "#"

        Component env system component server ->
            join "/" [ "#component", env, system, component, server ]


hashParser : Navigation.Location -> Page
hashParser location =
    Maybe.withDefault Components <| parseHash pageParser location


pageParser : Parser (Page -> a) a
pageParser =
    oneOf
        [ map Components top
        , map Component (s "component" </> string </> string </> string </> string)
        ]
