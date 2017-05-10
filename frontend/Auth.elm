port module Auth exposing (..)


port login : () -> Cmd msg


port loginResult : (String -> msg) -> Sub msg
