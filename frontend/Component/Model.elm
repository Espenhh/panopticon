module Component.Model exposing (..)


type alias Model =
    { environment : String
    , system : String
    , component : String
    , server : String
    , status : Status
    , details : String
    }


type Status
    = Info
    | Warn
    | Error
    | Missing
