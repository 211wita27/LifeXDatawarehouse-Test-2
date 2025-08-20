import React from "react";

const NavBar: React.FC = () => (
    <nav className="navbar navbar-expand navbar-lifex" aria-label="Main navigation">
        <div className="container-xxl">
            <a className="navbar-brand text-white fw-semibold" href="/index.html">Dashboard</a>
            <ul className="navbar-nav ms-2">
                <li className="nav-item"><a className="nav-link text-white" href="/create.html">Daten anlegen</a></li>
                <li className="nav-item"><a className="nav-link text-white" href="/reports.html">Reports</a></li>
            </ul>
        </div>
    </nav>
);
export default NavBar;