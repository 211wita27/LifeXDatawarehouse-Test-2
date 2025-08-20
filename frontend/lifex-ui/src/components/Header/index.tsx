import React from "react";
import Logo from "../Logo";
import NavBar from "../NavBar";

const Header: React.FC = () => (
    <header className="navbar-lifex py-2">
        <div className="container-xxl d-flex align-items-center gap-3">
            <Logo />
            <div className="flex-grow-1" />
        </div>
        <NavBar />
    </header>
);
export default Header;