{ stdenvNoCC, copyDesktopItems }:

stdenvNoCC.mkDerivation {
  pname = "termlink";
  version = "0.0.1";

  src = ./.;
  nativeBuildInputs = [ copyDesktopItems ];

  dontBuild = true;
  dontConfigure = true;
  dontPatchELF = true;  

  desktopItems = [ ./termlink.desktop ];

  installPhase = ''
    runHook preInstall
    install -D -t $out/bin -m775 termlink
    runHook postInstall
  '';
}
