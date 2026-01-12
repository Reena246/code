{
    "version": "2.0.0",
    "tasks": [
        {
            "label": "maven: clean install",
            "type": "shell",
            "command": "mvn",
            "args": [
                "clean",
                "install",
                "-DskipTests"
            ],
            "group": {
                "kind": "build",
                "isDefault": true
            },
            "presentation": {
                "reveal": "always",
                "panel": "shared"
            },
            "problemMatcher": []
        },
        {
            "label": "maven: spring-boot:run",
            "type": "shell",
            "command": "mvn",
            "args": [
                "spring-boot:run"
            ],
            "group": "build",
            "presentation": {
                "reveal": "always",
                "panel": "shared"
            },
            "problemMatcher": []
        },
        {
            "label": "maven: test",
            "type": "shell",
            "command": "mvn",
            "args": [
                "test"
            ],
            "group": "test",
            "presentation": {
                "reveal": "always",
                "panel": "shared"
            },
            "problemMatcher": []
        }
    ]
}
