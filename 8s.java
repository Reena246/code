{
    "version": "0.2.0",
    "configurations": [
        {
            "type": "java",
            "name": "Launch BadgeMate Application",
            "request": "launch",
            "mainClass": "com.company.badgemate.BadgeMateApplication",
            "projectName": "badgemate-access-control",
            "args": "",
            "vmArgs": "-Dspring.profiles.active=default",
            "env": {},
            "console": "internalConsole",
            "stopOnEntry": false
        },
        {
            "type": "java",
            "name": "Launch BadgeMate Application (Debug)",
            "request": "launch",
            "mainClass": "com.company.badgemate.BadgeMateApplication",
            "projectName": "badgemate-access-control",
            "args": "",
            "vmArgs": "-Dspring.profiles.active=default -Xdebug",
            "env": {},
            "console": "internalConsole",
            "stopOnEntry": false
        }
    ]
}
