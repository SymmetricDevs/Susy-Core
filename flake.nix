{
  description = "because nothing works on nixos from the first try";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
  };

  outputs = { self, nixpkgs }:
    let
      system = "x86_64-linux";
      pkgs = import nixpkgs { inherit system; };

      zulu8 = pkgs.zulu8;
      zulu17 = pkgs.zulu17;
      zulu21 = pkgs.zulu21;
      zulu25 = pkgs.zulu25;

      zuluPaths = "${zulu25},${zulu21},${zulu17},${zulu8}";
      gradle = pkgs.gradle_9;

      x11Libs = with pkgs; [
        libx11
        libxext
        libxcursor
        libxrandr
        libxxf86vm
        libxi
        libxinerama
        libxrender
        libxfixes
        libxcb
      ];
      x11LibPath = pkgs.lib.makeLibraryPath (x11Libs ++ [ pkgs.libGL ]);

    in
    {
      devShells.${system}.default = pkgs.mkShell {
        packages = [ zulu8 zulu17 zulu21 zulu25 gradle pkgs.xrandr pkgs.jq pkgs.git ];

        JAVA_HOME = "${zulu25}";

        shellHook = ''
          export LD_LIBRARY_PATH="${x11LibPath}''${LD_LIBRARY_PATH:+:$LD_LIBRARY_PATH}"
          grep -q "org.gradle.java.installations.paths" "$HOME/.gradle/gradle.properties" 2>/dev/null || \
            echo "org.gradle.java.installations.paths=${zuluPaths}" >> "$HOME/.gradle/gradle.properties"
        '';
      };
    };
}
