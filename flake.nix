{
  description = "Open hyperlinks from your terminal";
  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-22.05";
    flake-utils.url = "github:numtide/flake-utils";
  };
  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system: {
      packages = rec {
        termlink = (import nixpkgs { inherit system; }).callPackage ./termlink { };
        default = termlink;
      };
    });
}
