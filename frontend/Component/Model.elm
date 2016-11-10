module Component.Model exposing (..)


type alias Model =
    { id : String
    , component : String
    , environment : String
    , server : String
    , system : String
    , status : Status
    }


type Status
    = Info
    | Warn
    | Error
