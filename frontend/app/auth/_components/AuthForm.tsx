"use client";
import { Eye, EyeOff } from "lucide-react";
import { ChangeEvent, useState } from "react";

type User = {
  email?: string;
  password?: string;
  firstName?: string;
  lastName?: string;
};

type AuthFormProps = {
  title: string;
  subtitle: string;
  buttonText: string;
  fields: Fields[];
  isLogin: boolean;
};

const AuthForm = ({ title, subtitle, buttonText, fields, isLogin }: AuthFormProps) => {
  const [user, setUser] = useState<User>({});
  const [showPassword, setShowPassword] = useState(false);

  const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
    setUser({
      ...user,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async () => {
    try {
      const res = await fetch(
        `http://localhost:8080/api/auth/${isLogin ? "login" : "signup"}`,
        {
          method: "POST",
          headers: {
            "content-type": "application/json",
          },
          body: JSON.stringify(user),
        },
      );
      const data = await res.json();
      console.log(data);
    } catch (error) {
      console.error("error in signing up.", error);
    }
  };

  return (
    <div className="md:w-1/2 w-full h-full flex items-center flex-col p-4 gap-4 justify-center text-black text-center overflow-scroll">
      <div className="flex flex-col items-center">
        <h2 className="font-semibold text-2xl">{title}</h2>
        <p className="font-extralight text-xs text-black/50">{subtitle}</p>
      </div>
      <div className="flex flex-col gap-4 w-3/4 justify-evenly">
        {!isLogin && (
          <div className="grid grid-cols-2 gap-4">
            <input
              name="firstName"
              placeholder="First Name"
              type="text"
              onChange={(e) => handleChange(e)}
              className="rounded-md bg-slate-200 px-4 py-2 placeholder:text-sm"
            ></input>
            <input
              name="lastName"
              placeholder="Last Name"
              type="text"
              onChange={(e) => handleChange(e)}
              className="rounded-md bg-slate-200 px-4 py-2 placeholder:text-sm"
            ></input>
          </div>
        )}
        {fields.map((f, i) =>
          f.type == "password" ? (
            <div key={i} className="relative w-full flex-1 z-0">
              <input
                name={f.name}
                placeholder={f.placeholder}
                type={showPassword ? "text" : "password"}
                onChange={(e) => handleChange(e)}
                className="rounded-md bg-slate-200 px-4 py-2 placeholder:text-sm w-full"
              ></input>
              <button
                className="absolute right-4 top-1/2 -translate-y-1/2 cursor-pointer text-black/50 hover:text-black"
                onClick={() => setShowPassword(x => !x)}
              >
                {showPassword ? <EyeOff/>:<Eye/> }
              </button>
            </div>
          ) : (
            <input
              key={i}
              name={f.name}
              placeholder={f.placeholder}
              type={f.type}
              onChange={(e) => handleChange(e)}
              className="rounded-md bg-slate-200 px-4 py-2 placeholder:text-sm"
            ></input>
          ),
        )}
      </div>
      <button
        onClick={handleSubmit}
        className="cursor-pointer px-8 py-2 bg-blue-900 rounded-lg text-white font-medium"
      >
        {buttonText}
      </button>
    </div>
  );
};

export default AuthForm;
